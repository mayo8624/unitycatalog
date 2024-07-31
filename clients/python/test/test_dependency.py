# coding: utf-8

"""
    Unity Catalog API

    No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)

    The version of the OpenAPI document: 0.1
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


import unittest

from unitycatalog.models.dependency import Dependency

class TestDependency(unittest.TestCase):
    """Dependency unit test stubs"""

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def make_instance(self, include_optional) -> Dependency:
        """Test Dependency
            include_optional is a boolean, when False only required
            params are included, when True both required and
            optional params are included """
        # uncomment below to create an instance of `Dependency`
        """
        model = Dependency()
        if include_optional:
            return Dependency(
                table = unitycatalog.models.table_dependency.TableDependency(
                    table_full_name = '', ),
                function = unitycatalog.models.function_dependency.FunctionDependency(
                    function_full_name = '', )
            )
        else:
            return Dependency(
        )
        """

    def testDependency(self):
        """Test Dependency"""
        # inst_req_only = self.make_instance(include_optional=False)
        # inst_req_and_optional = self.make_instance(include_optional=True)

if __name__ == '__main__':
    unittest.main()
